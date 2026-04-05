import { useEffect, useState } from 'react'

const MODAL_EXIT_DURATION_MS = 240

function AnimatedModal({
  isOpen,
  onClose,
  className = '',
  labelledBy,
  background,
  children,
}) {
  const [isRendered, setIsRendered] = useState(isOpen)
  const [isClosing, setIsClosing] = useState(false)

  useEffect(() => {
    if (isOpen) {
      setIsRendered(true)
      setIsClosing(false)
      return undefined
    }

    if (!isRendered) {
      return undefined
    }

    setIsClosing(true)

    const timeoutId = window.setTimeout(() => {
      setIsRendered(false)
      setIsClosing(false)
    }, MODAL_EXIT_DURATION_MS)

    return () => window.clearTimeout(timeoutId)
  }, [isOpen, isRendered])

  useEffect(() => {
    if (!isRendered) {
      return undefined
    }

    const handleKeyDown = (event) => {
      if (event.key === 'Escape' && !isClosing) {
        onClose()
      }
    }

    window.addEventListener('keydown', handleKeyDown)

    return () => window.removeEventListener('keydown', handleKeyDown)
  }, [isClosing, isRendered, onClose])

  if (!isRendered) {
    return null
  }

  const modalClassName = ['expense-modal', className, isClosing ? 'is-closing' : '']
    .filter(Boolean)
    .join(' ')

  return (
    <div
      className={`modal-overlay${isClosing ? ' is-closing' : ''}`}
      onClick={() => {
        if (!isClosing) {
          onClose()
        }
      }}
      role="presentation"
    >
      <div
        className={modalClassName}
        onClick={(event) => event.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-labelledby={labelledBy}
        style={{ '--modal-background': `url(${background})` }}
      >
        {children}
      </div>
    </div>
  )
}

export default AnimatedModal
